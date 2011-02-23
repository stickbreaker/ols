/*
 * OpenBench LogicSniffer / SUMP project 
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
 *
 * Copyright (C) 2006-2010 Michael Poppitz, www.sump.org
 * Copyright (C) 2010-2011 J.W. Janssen, www.lxtreme.nl
 */
package nl.lxtreme.ols.util;


import java.util.*;


/**
 * Provides some common string utilities.
 */
public final class StringUtils
{
  // CONSTRUCTORS

  /**
   * Creates a new StringUtils instance.
   */
  private StringUtils()
  {
    // NO-op
  }

  // METHODS

  /**
   * converts an integer to a bin string with leading zeros
   * 
   * @param aValue
   *          integer value for conversion
   * @param aFieldWidth
   *          number of charakters in field
   * @return a nice string
   */
  public static String integerToBinString( final int aValue, final int aFieldWidth )
  {
    // first build a mask to cut off the signed extension
    final int mask = ( int )Math.pow( 2.0, aFieldWidth ) - 1;

    StringBuilder sb = new StringBuilder( Integer.toBinaryString( aValue & mask ) );

    int numberOfLeadingZeros = aFieldWidth - sb.length();
    if ( numberOfLeadingZeros < 0 )
    {
      numberOfLeadingZeros = 0;
    }
    if ( numberOfLeadingZeros > aFieldWidth )
    {
      numberOfLeadingZeros = aFieldWidth;
    }

    if ( numberOfLeadingZeros > 0 )
    {
      for ( ; numberOfLeadingZeros > 0; numberOfLeadingZeros-- )
      {
        sb.insert( 0, '0' );
      }
    }

    return sb.toString();
  }

  /**
   * converts an integer to a hex string with leading zeros
   * 
   * @param aValue
   *          integer value for conversion
   * @param aFieldWidth
   *          number of charakters in field
   * @return a nice string
   */
  public static String integerToHexString( final int aValue, final int aFieldWidth )
  {
    // first build a mask to cut off the signed extension
    final int mask = ( int )Math.pow( 16.0, aFieldWidth ) - 1;

    String str = Integer.toHexString( aValue & mask );
    int numberOfLeadingZeros = aFieldWidth - str.length();
    if ( numberOfLeadingZeros < 0 )
    {
      numberOfLeadingZeros = 0;
    }
    if ( numberOfLeadingZeros > aFieldWidth )
    {
      numberOfLeadingZeros = aFieldWidth;
    }
    char zeros[] = new char[numberOfLeadingZeros];
    for ( int i = 0; i < zeros.length; i++ )
    {
      zeros[i] = '0';
    }
    String ldz = new String( zeros );
    return ( new String( ldz + str ) );
  }

  /**
   * Returns whether the given string is actually empty, meaning
   * <code>null</code> or an empty string.
   * 
   * @param aValue
   *          the string value to check for "emptyness", can be
   *          <code>null</code>.
   * @return <code>true</code> if the given string is empty, <code>false</code>
   *         otherwise.
   */
  public static boolean isEmpty( final String aValue )
  {
    return ( aValue == null ) || aValue.trim().isEmpty();
  }

  /**
   * Tokenizes a given input stream and breaks it into parts on the given
   * delimiters.
   * 
   * @param aInput
   *          the input string to tokenize, can be <code>null</code>;
   * @param aDelimiters
   *          the delimiter <em>characters</em> to break the given input on,
   *          cannot be <code>null</code>.
   * @return the individual tokens of the given input, or <code>null</code> if
   *         the original input was <code>null</code>.
   */
  public static String[] tokenize( final String aInput, final String aDelimiters )
  {
    if ( aInput == null )
    {
      return null;
    }

    final List<String> result = new ArrayList<String>();

    final StringTokenizer tokenizer = new StringTokenizer( aInput, aDelimiters, false /* returnDelims */);
    while ( tokenizer.hasMoreTokens() )
    {
      String token = tokenizer.nextToken();
      result.add( token.trim() );
    }

    return result.toArray( new String[result.size()] );
  }

  /**
   * Tokenizes a given input stream and breaks it into parts on the given
   * delimiters. The tokens are considered to be double quoted strings.
   * 
   * @param aInput
   *          the input string to tokenize, can be <code>null</code>;
   * @param aDelimiters
   *          the delimiter <em>characters</em> to break the given input on,
   *          cannot be <code>null</code>.
   * @return the individual tokens of the given input, or <code>null</code> if
   *         the original input was <code>null</code>.
   */
  public static String[] tokenizeQuotedStrings( final String aInput, final String aDelimiters )
  {
    if ( aInput == null )
    {
      return null;
    }

    final List<String> result = new ArrayList<String>();

    String lastDelimiter = "";

    final StringTokenizer tokenizer = new StringTokenizer( aInput, aDelimiters, true /* returnDelims */);
    while ( tokenizer.hasMoreTokens() )
    {
      String token = tokenizer.nextToken();

      if ( isDelimiter( token, aDelimiters ) )
      {
        lastDelimiter = lastDelimiter.concat( token );
        continue;
      }

      if ( !result.isEmpty() && token.endsWith( "\"" ) )
      {
        // Hmm, maybe a broken token? Check the previous one...
        final int lastIdx = result.size() - 1;
        String previous = result.get( lastIdx );
        if ( previous.startsWith( "\"" ) && !previous.endsWith( "\"" ) )
        {
          // Yes, broken token! Concat it...
          previous = previous.concat( lastDelimiter ).concat( token );
          result.set( lastIdx, previous );
          continue;
        }
      }

      result.add( token );
      lastDelimiter = "";
    }

    // Last step is to unquote all results...
    final ListIterator<String> iter = result.listIterator();
    while ( iter.hasNext() )
    {
      final String token = iter.next();
      iter.set( unquote( token ) );
    }

    return result.toArray( new String[result.size()] );
  }

  /**
   * Removes all double quotes from the given input.
   * 
   * @param aInput
   *          the input string to remove the quotes from, may be
   *          <code>null</code>.
   * @return the unquoted version of the given input, or <code>null</code> if
   *         the original input was <code>null</code>.
   * @see #unquote(String, char)
   */
  public static String unquote( final String aInput )
  {
    return unquote( aInput, '"' );
  }

  /**
   * Removes all quotes, denoted by the given character, from the given input.
   * 
   * @param aInput
   *          the input string to remove the quotes from, may be
   *          <code>null</code>;
   * @param aQuoteChar
   *          the quote character to remove.
   * @return the unquoted version of the given input, or <code>null</code> if
   *         the original input was <code>null</code>. If the given input
   *         contained leading and/or trailing whitespace, it is removed from
   *         the result.
   */
  public static String unquote( final String aInput, final char aQuoteChar )
  {
    if ( aInput == null )
    {
      return null;
    }

    final String quoteChar = Character.toString( aQuoteChar );

    String result = aInput.trim();
    if ( result.startsWith( quoteChar ) && result.endsWith( quoteChar ) )
    {
      result = result.substring( 1, result.length() - 1 );
    }
    return result;
  }

  /**
   * @param aInput
   * @param aDelimiters
   * @return
   */
  private static boolean isDelimiter( final String aInput, final String aDelimiters )
  {
    final String regex = "^[" + aDelimiters + "]+$";
    return aInput.matches( regex );
  }

}