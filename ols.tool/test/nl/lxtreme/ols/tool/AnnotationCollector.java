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
 * Copyright (C) 2010-2012 J.W. Janssen, www.lxtreme.nl
 */
package nl.lxtreme.ols.tool;


import static nl.lxtreme.ols.tool.api.AnnotationHelper.*;
import java.util.*;

import junit.framework.*;

import nl.lxtreme.ols.common.annotation.*;


/**
 * Provides a collector for annotations, splitting them out to type and keep
 * them sorted on their time stamps.
 */
public class AnnotationCollector implements AnnotationListener
{
  // VARIABLES

  public final SortedSet<DataAnnotation> dataAnnotations;
  public final SortedSet<Annotation> annotations;

  // CONSTRUCTORS

  /**
   * Creates a new {@link AnnotationCollector} instance.
   */
  public AnnotationCollector()
  {
    this.dataAnnotations = new TreeSet<DataAnnotation>();
    this.annotations = new TreeSet<Annotation>();
  }

  /**
   * Asserts that a series of symbols is found in the contained annotations,
   * starting from the first found data annotation containing the "symbol=true"
   * property.
   * 
   * @param aSymbols
   *          the symbols to test for, cannot be <code>null</code>.
   */
  public void assertDataSymbols( final int... aSymbols )
  {
    int idx = 0;
    for ( DataAnnotation annotation : this.dataAnnotations )
    {
      Map<String, Object> props = annotation.getProperties();
      if ( Boolean.TRUE.equals( props.get( KEY_SYMBOL ) ) )
      {
        Integer data = ( Integer )annotation.getData();
        Assert.assertEquals( "Symbol @ " + idx, aSymbols[idx++], data.intValue() );
      }
      if ( idx >= aSymbols.length )
      {
        // Avoid index out of bounds errors...
        break;
      }
    }
    Assert.assertEquals( "Not all data symbols were seen!", aSymbols.length, idx );
  }

  /**
   * Asserts that a series of symbols is found in the contained annotations,
   * starting from the first found data annotation containing the "symbol=true"
   * property.
   * 
   * @param aChannelIdx
   *          the channel index to test the symbols for, >= 0;
   * @param aSymbols
   *          the symbols to test for, cannot be <code>null</code>.
   */
  public void assertDataSymbolsOn( final int aChannelIdx, final int... aSymbols )
  {
    int idx = 0;
    for ( DataAnnotation annotation : this.dataAnnotations )
    {
      Map<String, Object> props = annotation.getProperties();
      if ( ( aChannelIdx == annotation.getChannelIndex() ) && Boolean.TRUE.equals( props.get( KEY_SYMBOL ) ) )
      {
        Integer data = ( Integer )annotation.getData();
        Assert.assertEquals( "Symbol @ " + idx, aSymbols[idx++], data.intValue() );
      }
      if ( idx >= aSymbols.length )
      {
        // Avoid index out of bounds errors...
        break;
      }
    }
    Assert.assertEquals( "Not all data symbols were seen!", aSymbols.length, idx );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clearAnnotations()
  {
    this.annotations.clear();
    this.dataAnnotations.clear();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clearAnnotations( final int aChannelIdx )
  {
    Iterator<DataAnnotation> dataAnnotationIter = this.dataAnnotations.iterator();
    while ( dataAnnotationIter.hasNext() )
    {
      DataAnnotation annotation = dataAnnotationIter.next();
      if ( annotation.getChannelIndex() == aChannelIdx )
      {
        dataAnnotationIter.remove();
      }
    }

    Iterator<Annotation> annotationIter = this.annotations.iterator();
    while ( annotationIter.hasNext() )
    {
      Annotation annotation = annotationIter.next();
      if ( annotation.getChannelIndex() == aChannelIdx )
      {
        annotationIter.remove();
      }
    }
  }

  /**
   * Counts all annotations with the given symbol.
   * 
   * @param aSymbol
   *          the symbol to search for, cannot be <code>null</code>.
   * @return the number of found annotations, >= 0.
   */
  public int countDataAnnotations( final Object aSymbol, final Object... aProperties )
  {
    int result = 0;
    for ( DataAnnotation annotation : this.dataAnnotations )
    {
      if ( aSymbol.equals( annotation.getData() ) && propertiesMatch( annotation, aProperties ) )
      {
        result++;
      }
    }
    return result;
  }

  /**
   * Counts the number of data errors, that is, those annotations that are
   * annotated with an "error=true" property.
   * 
   * @return the number of data errors, >= 0.
   */
  public int countDataErrors()
  {
    int count = 0;
    for ( DataAnnotation annotation : this.dataAnnotations )
    {
      Map<String, Object> props = annotation.getProperties();
      if ( Boolean.TRUE.equals( props.get( KEY_ERROR ) ) )
      {
        count++;
      }
    }
    return count;
  }

  /**
   * Counts the number of data symbols, that is, those annotations that are
   * annotated with an "symbol=true" property.
   * 
   * @return the number of data symbols, >= 0.
   */
  public int countSymbols()
  {
    int count = 0;
    for ( DataAnnotation annotation : this.dataAnnotations )
    {
      Map<String, Object> props = annotation.getProperties();
      if ( Boolean.TRUE.equals( props.get( KEY_SYMBOL ) ) )
      {
        count++;
      }
    }
    return count;
  }

  /**
   * Counts the number of data symbols, that is, those annotations that are
   * annotated with an "symbol=true" property.
   * 
   * @param aChannelIdx
   *          the index of the channel to count the symbols for, >= 0.
   * @return the number of data symbols, >= 0.
   */
  public int countSymbolsOn( final int aChannelIdx )
  {
    int count = 0;
    for ( DataAnnotation annotation : this.dataAnnotations )
    {
      Map<String, Object> props = annotation.getProperties();
      if ( ( aChannelIdx == annotation.getChannelIndex() ) && Boolean.TRUE.equals( props.get( KEY_SYMBOL ) ) )
      {
        count++;
      }
    }
    return count;
  }

  /**
   * Returns an annotation with the given symbol.
   * 
   * @param aSymbol
   *          the symbol to search for, cannot be <code>null</code>.
   * @return the first annotation (in time) that contains the given symbol, or
   *         <code>null</code> if no annotation matched the given symbol.
   */
  public DataAnnotation getDataAnnotation( final Object aSymbol )
  {
    for ( DataAnnotation annotation : this.dataAnnotations )
    {
      if ( aSymbol.equals( annotation.getData() ) )
      {
        return annotation;
      }
    }
    return null;
  }

  /**
   * Returns an annotation with the given class.
   * 
   * @param aClass
   *          the class to search for, cannot be <code>null</code>.
   * @return the first annotation (in time) that is of the given class, or
   *         <code>null</code> if no annotation matched the given class.
   */
  @SuppressWarnings( "unchecked" )
  public <T extends DataAnnotation> T getDataAnnotation( final Class<T> aClass )
  {
    for ( DataAnnotation annotation : this.dataAnnotations )
    {
      if ( aClass.equals( annotation.getClass() ) )
      {
        return ( T )annotation;
      }
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onAnnotation( final Annotation aAnnotation )
  {
    if ( aAnnotation instanceof DataAnnotation )
    {
      this.dataAnnotations.add( ( DataAnnotation )aAnnotation );
    }
    else
    {
      this.annotations.add( aAnnotation );
    }
  }

  /**
   * @param aAnnotation
   * @param aExpectedProperties
   * @return
   */
  private boolean propertiesMatch( final DataAnnotation aAnnotation, final Object[] aExpectedProperties )
  {
    Map<String, Object> properties = aAnnotation.getProperties();

    for ( int i = 0; i < aExpectedProperties.length; i += 2 )
    {
      String key = String.valueOf( aExpectedProperties[i] );
      Object expectedValue = aExpectedProperties[i + 1];

      if ( !expectedValue.equals( properties.get( key ) ) )
      {
        return false;
      }
    }

    return true;
  }
}